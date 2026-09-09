package com.zayaai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FormulaViewModel(private val repository: AiZayaRepository) : ViewModel() {

    val formulas: StateFlow<List<FormulaEntity>> = repository.allFormulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedFormulas()
    }

    private fun seedFormulas() {
        viewModelScope.launch {
            val existing = repository.allFormulas.first()
            if (existing.size < 10) {
                // Clear old seed data and insert comprehensive formula library
                repository.deleteAllFormulas()

                val allFormulas = listOf(
                    // --- MATHEMATICS ---
                    FormulaEntity(category = "Mathematics", name = "Quadratic Formula", formula = "x = (-b ± √(b² - 4ac)) / (2a)", description = "Finds the roots of a quadratic equation ax² + bx + c = 0."),
                    FormulaEntity(category = "Mathematics", name = "Pythagorean Theorem", formula = "a² + b² = c²", description = "In a right triangle, the square of the hypotenuse equals the sum of squares of the other two sides."),
                    FormulaEntity(category = "Mathematics", name = "Distance Formula", formula = "d = √((x2 - x1)² + (y2 - y1)²)", description = "Calculates the distance between two points in a coordinate plane."),
                    FormulaEntity(category = "Mathematics", name = "Slope Formula", formula = "m = (y2 - y1) / (x2 - x1)", description = "Calculates the slope of a line passing through two points."),
                    FormulaEntity(category = "Mathematics", name = "Area of Circle", formula = "A = π r²", description = "Calculates the area of a circle with radius r."),
                    FormulaEntity(category = "Mathematics", name = "Circumference of Circle", formula = "C = 2π r", description = "Calculates the circumference of a circle with radius r."),
                    FormulaEntity(category = "Mathematics", name = "Area of Triangle", formula = "A = 1/2 × base × height", description = "Calculates the area of a triangle given its base and height."),
                    FormulaEntity(category = "Mathematics", name = "Heron's Formula", formula = "A = √(s(s-a)(s-b)(s-c))", description = "Area of a triangle where s = (a+b+c)/2 is the semi-perimeter."),
                    FormulaEntity(category = "Mathematics", name = "Volume of Sphere", formula = "V = (4/3) π r³", description = "Calculates the volume of a sphere with radius r."),
                    FormulaEntity(category = "Mathematics", name = "Surface Area of Sphere", formula = "SA = 4 π r²", description = "Calculates the surface area of a sphere with radius r."),
                    FormulaEntity(category = "Mathematics", name = "Volume of Cylinder", formula = "V = π r² h", description = "Calculates the volume of a cylinder with radius r and height h."),
                    FormulaEntity(category = "Mathematics", name = "Volume of Cone", formula = "V = (1/3) π r² h", description = "Calculates the volume of a cone with radius r and height h."),
                    FormulaEntity(category = "Mathematics", name = "Logarithm Product Rule", formula = "log(ab) = log(a) + log(b)", description = "The logarithm of a product equals the sum of the logarithms."),
                    FormulaEntity(category = "Mathematics", name = "Logarithm Power Rule", formula = "log(a^n) = n × log(a)", description = "The logarithm of a power equals the exponent times the logarithm of the base."),
                    FormulaEntity(category = "Mathematics", name = "Compound Interest", formula = "A = P(1 + r/n)^(nt)", description = "A = final amount, P = principal, r = rate, n = times compounded per year, t = years."),
                    FormulaEntity(category = "Mathematics", name = "Simple Interest", formula = "I = PRT", description = "I = interest, P = principal, R = rate per year, T = time in years."),
                    FormulaEntity(category = "Mathematics", name = "Arithmetic Sequence Sum", formula = "S_n = n/2 × (a_1 + a_n)", description = "Sum of first n terms of an arithmetic sequence."),
                    FormulaEntity(category = "Mathematics", name = "Geometric Sequence Sum", formula = "S_n = a_1(1 - r^n) / (1 - r)", description = "Sum of first n terms of a geometric sequence where r ≠ 1."),
                    FormulaEntity(category = "Mathematics", name = "Binomial Theorem", formula = "(a+b)^n = Σ C(n,k) a^(n-k) b^k", description = "Expands the power of a binomial as a sum of terms."),
                    FormulaEntity(category = "Mathematics", name = "Permutation", formula = "P(n,r) = n! / (n-r)!", description = "Number of ways to arrange r items from n items in order."),
                    FormulaEntity(category = "Mathematics", name = "Combination", formula = "C(n,r) = n! / (r!(n-r)!)", description = "Number of ways to choose r items from n items without order."),
                    FormulaEntity(category = "Mathematics", name = "Sin Rule", formula = "a/sinA = b/sinB = c/sinC", description = "Relates the sides and angles of any triangle."),
                    FormulaEntity(category = "Mathematics", name = "Cosine Rule", formula = "c² = a² + b² - 2ab cos(C)", description = "Relates the sides and an angle of any triangle."),
                    FormulaEntity(category = "Mathematics", name = "Derivative (Power Rule)", formula = "d/dx [x^n] = n x^(n-1)", description = "The derivative of x to the power n."),
                    FormulaEntity(category = "Mathematics", name = "Integration (Power Rule)", formula = "∫ x^n dx = x^(n+1)/(n+1) + C", description = "The integral of x to the power n (n ≠ -1)."),
                    FormulaEntity(category = "Mathematics", name = "Mean", formula = "x̄ = Σx_i / n", description = "Average of a set of numbers."),
                    FormulaEntity(category = "Mathematics", name = "Standard Deviation", formula = "σ = √(Σ(x_i - x̄)² / n)", description = "Measures the spread of data from the mean."),

                    // --- PHYSICS ---
                    FormulaEntity(category = "Physics", name = "Einstein's Mass-Energy", formula = "E = mc²", description = "Energy equals mass times the speed of light squared."),
                    FormulaEntity(category = "Physics", name = "Newton's Second Law", formula = "F = ma", description = "Force equals mass times acceleration."),
                    FormulaEntity(category = "Physics", name = "Weight", formula = "W = mg", description = "Weight equals mass times gravitational acceleration (g ≈ 9.8 m/s²)."),
                    FormulaEntity(category = "Physics", name = "Speed", formula = "v = d / t", description = "Speed equals distance divided by time."),
                    FormulaEntity(category = "Physics", name = "Acceleration", formula = "a = (v - u) / t", description = "Acceleration equals change in velocity divided by time."),
                    FormulaEntity(category = "Physics", name = "Equation of Motion 1", formula = "v = u + at", description = "Final velocity = initial velocity + acceleration × time."),
                    FormulaEntity(category = "Physics", name = "Equation of Motion 2", formula = "s = ut + 1/2 at²", description = "Displacement = initial velocity × time + 1/2 × acceleration × time²."),
                    FormulaEntity(category = "Physics", name = "Equation of Motion 3", formula = "v² = u² + 2as", description = "Final velocity² = initial velocity² + 2 × acceleration × displacement."),
                    FormulaEntity(category = "Physics", name = "Kinetic Energy", formula = "KE = 1/2 m v²", description = "Energy of a moving object. m = mass, v = velocity."),
                    FormulaEntity(category = "Physics", name = "Potential Energy", formula = "PE = mgh", description = "Energy stored due to position. m = mass, g = gravity, h = height."),
                    FormulaEntity(category = "Physics", name = "Work Done", formula = "W = F × d × cos(θ)", description = "Work equals force times displacement times cosine of the angle."),
                    FormulaEntity(category = "Physics", name = "Power", formula = "P = W / t", description = "Power equals work done divided by time."),
                    FormulaEntity(category = "Physics", name = "Momentum", formula = "p = mv", description = "Momentum equals mass times velocity."),
                    FormulaEntity(category = "Physics", name = "Impulse", formula = "J = F × t = Δp", description = "Impulse equals force times time, or change in momentum."),
                    FormulaEntity(category = "Physics", name = "Pressure", formula = "P = F / A", description = "Pressure equals force divided by area."),
                    FormulaEntity(category = "Physics", name = "Density", formula = "ρ = m / V", description = "Density equals mass divided by volume."),
                    FormulaEntity(category = "Physics", name = "Ohm's Law", formula = "V = IR", description = "Voltage equals current times resistance."),
                    FormulaEntity(category = "Physics", name = "Electrical Power", formula = "P = VI = I²R = V²/R", description = "Electrical power = voltage × current."),
                    FormulaEntity(category = "Physics", name = "Coulomb's Law", formula = "F = k q1 q2 / r²", description = "Force between two charges. k ≈ 9×10⁹ N·m²/C²."),
                    FormulaEntity(category = "Physics", name = "Wave Speed", formula = "v = f λ", description = "Wave speed = frequency × wavelength."),
                    FormulaEntity(category = "Physics", name = "Frequency", formula = "f = 1/T", description = "Frequency is the reciprocal of the time period."),
                    FormulaEntity(category = "Physics", name = "Snell's Law", formula = "n1 sin θ1 = n2 sin θ2", description = "Relates angles and refractive indices when light passes between media."),
                    FormulaEntity(category = "Physics", name = "Gravitational Force", formula = "F = G m1 m2 / r²", description = "Newton's law of universal gravitation."),
                    FormulaEntity(category = "Physics", name = "Centripetal Force", formula = "F = m v² / r", description = "Force directed towards the center of circular motion."),
                    FormulaEntity(category = "Physics", name = "Period of Pendulum", formula = "T = 2π √(L/g)", description = "Time period of a simple pendulum. L = length, g = gravity."),
                    FormulaEntity(category = "Physics", name = "Hooke's Law", formula = "F = -kx", description = "Force of a spring = spring constant × displacement."),
                    FormulaEntity(category = "Physics", name = "Thermal Energy", formula = "Q = mc ΔT", description = "Heat energy = mass × specific heat capacity × change in temperature."),
                    FormulaEntity(category = "Physics", name = "Ideal Gas Law", formula = "PV = nRT", description = "Relates pressure, volume, moles of gas, and temperature."),

                    // --- CHEMISTRY ---
                    FormulaEntity(category = "Chemistry", name = "Moles Formula", formula = "n = m / M", description = "Number of moles = mass / molar mass."),
                    FormulaEntity(category = "Chemistry", name = "Molarity", formula = "M = n / V", description = "Molarity = moles of solute / volume of solution in liters."),
                    FormulaEntity(category = "Chemistry", name = "Dilution Formula", formula = "M1 V1 = M2 V2", description = "Concentration × volume before dilution = concentration × volume after."),
                    FormulaEntity(category = "Chemistry", name = "pH Formula", formula = "pH = -log[H+]", description = "pH is the negative logarithm of hydrogen ion concentration."),
                    FormulaEntity(category = "Chemistry", name = "pOH Formula", formula = "pOH = -log[OH-]", description = "pOH is the negative logarithm of hydroxide ion concentration."),
                    FormulaEntity(category = "Chemistry", name = "pH + pOH", formula = "pH + pOH = 14", description = "At 25°C, pH and pOH always add up to 14."),
                    FormulaEntity(category = "Chemistry", name = "Ideal Gas Law", formula = "PV = nRT", description = "P = pressure, V = volume, n = moles, R = gas constant, T = temperature in Kelvin."),
                    FormulaEntity(category = "Chemistry", name = "Boyle's Law", formula = "P1 V1 = P2 V2", description = "At constant temperature, pressure and volume are inversely proportional."),
                    FormulaEntity(category = "Chemistry", name = "Charles's Law", formula = "V1 / T1 = V2 / T2", description = "At constant pressure, volume and temperature are directly proportional."),
                    FormulaEntity(category = "Chemistry", name = "Avogadro's Number", formula = "N_A = 6.022 × 10^23", description = "The number of atoms/molecules in one mole of a substance."),
                    FormulaEntity(category = "Chemistry", name = "Mass Percent", formula = "% = (mass of solute / mass of solution) × 100", description = "Percentage of a solute in a solution by mass."),
                    FormulaEntity(category = "Chemistry", name = "Rate of Reaction", formula = "Rate = -Δ[R]/Δt = Δ[P]/Δt", description = "Rate of reaction = change in concentration over change in time."),
                    FormulaEntity(category = "Chemistry", name = "Equilibrium Constant", formula = "Keq = [Products]^p / [Reactants]^r", description = "Ratio of product concentrations to reactant concentrations at equilibrium."),
                    FormulaEntity(category = "Chemistry", name = "Gibbs Free Energy", formula = "ΔG = ΔH - TΔS", description = "Predicts spontaneity. ΔH = enthalpy, T = temperature, ΔS = entropy."),
                    FormulaEntity(category = "Chemistry", name = "Enthalpy Change", formula = "ΔH = H(products) - H(reactants)", description = "Change in heat content during a reaction."),
                    FormulaEntity(category = "Chemistry", name = "Density of Gas", formula = "ρ = PM / RT", description = "Gas density = (Pressure × Molar mass) / (Gas constant × Temperature)."),
                    FormulaEntity(category = "Chemistry", name = "Percent Yield", formula = "% Yield = (Actual / Theoretical) × 100", description = "Efficiency of a chemical reaction."),
                    FormulaEntity(category = "Chemistry", name = "Molality", formula = "m = moles of solute / kg of solvent", description = "Molality is moles of solute per kilogram of solvent."),
                    FormulaEntity(category = "Chemistry", name = "Nernst Equation", formula = "E = E° - (RT/nF) ln Q", description = "Calculates electrode potential under non-standard conditions."),
                    FormulaEntity(category = "Chemistry", name = "Half-Life", formula = "t1/2 = 0.693 / k", description = "Time for half of a substance to decay/react. k = rate constant.")
                )

                allFormulas.forEach { repository.insertFormula(it) }
            }
        }
    }
}

